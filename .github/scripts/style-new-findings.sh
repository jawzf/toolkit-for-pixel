#!/usr/bin/env bash
# Usage: style-new-findings.sh <base-ref>   (run from the repo root)
# Fails when ktlint or detekt report more findings in a changed Kotlin file than the same
# file had at <base-ref>, so old upstream findings never block a change.
# A file whose base version already breaks ktlint's indent rule skips that rule: new lines
# that match the surrounding code would otherwise count as new findings.
# KTLINT and DETEKT override the commands (CI passes `java -jar …`).
# Plain bash 3.2 (macOS) compatible: no mapfile, no set -u (empty arrays trip it).
set -o pipefail

BASE=${1:?base ref}
CFG="$PWD/.github/lint"
KTLINT=${KTLINT:-ktlint}
DETEKT=${DETEKT:-detekt}

kt=()
while IFS= read -r f; do kt+=("$f"); done < <(git diff --name-only --diff-filter=d "$BASE" -- '*.kt')
if (( ${#kt[@]} == 0 )); then echo "style: no Kotlin files changed"; exit 0; fi

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
# A renamed file is compared with its old path at the base, stored under the new name.
renames=$(git diff -M --name-status --diff-filter=R "$BASE" | awk -F'\t' '{print $3"\t"$2}')
base_path() { local p; p=$(printf '%s\n' "$renames" | awk -F'\t' -v f="$1" '$1==f{print $2; exit}'); echo "${p:-$1}"; }

base_kt=()
for f in "${kt[@]}"; do
  b=$(base_path "$f")
  if git cat-file -e "$BASE:$b" 2>/dev/null; then
    mkdir -p "$tmp/base/$(dirname "$f")"
    git show "$BASE:$b" > "$tmp/base/$f"
    base_kt+=("$f")
  fi
done

ktlint_run() { $KTLINT --relative --editorconfig="$CFG/ktlint.editorconfig" "$@" 2>/dev/null; }

# detekt exits 1 (error) or 3 (bad config) when it can't run; treat that as a failure, not a pass.
detekt_run() {
  local list out rc
  list=$(IFS=,; echo "$*")
  out=$($DETEKT --input "$list" --config "$CFG/detekt.yml" --build-upon-default-config 2>&1)
  rc=$?
  if (( rc == 1 || rc == 3 )); then echo "$out" | head -20 >&2; return 1; fi
  # No findings is a pass: grep's exit 1 must not look like a detekt failure.
  echo "$out" | grep -E '\.kt:[0-9]+' || true
}

ktlint_head=$(ktlint_run "${kt[@]}")
ktlint_base=""
(( ${#base_kt[@]} )) && ktlint_base=$(cd "$tmp/base" && ktlint_run "${base_kt[@]}")
detekt_head=$(detekt_run "${kt[@]}") || { echo "FAIL detekt could not run"; exit 1; }
detekt_base=""
if (( ${#base_kt[@]} )); then
  detekt_base=$(cd "$tmp/base" && detekt_run "${base_kt[@]}") || { echo "FAIL detekt could not run on base"; exit 1; }
fi

# Findings for one file: ktlint prints "path:line:col: …", detekt prints "… at /abs/path:line:col …".
for_file() { grep -E "(^|/)$(printf '%s' "$2" | sed 's/[.[\*^$/]/\\&/g'):[0-9]+" <<< "$1"; }
count() { [[ -z "$1" ]] && echo 0 || grep -c . <<< "$1"; }

worse=0
for f in "${kt[@]}"; do
  kh=$(for_file "$ktlint_head" "$f"); kb=$(for_file "$ktlint_base" "$f")
  if grep -qF '(standard:indent)' <<< "$kb"; then
    kh=$(grep -vF '(standard:indent)' <<< "$kh"); kb=$(grep -vF '(standard:indent)' <<< "$kb")
  fi
  dh=$(for_file "$detekt_head" "$f"); db=$(for_file "$detekt_base" "$f")
  for tool in ktlint detekt; do
    if [[ $tool == ktlint ]]; then now=$(count "$kh"); before=$(count "$kb"); lines=$kh
    else now=$(count "$dh"); before=$(count "$db"); lines=$dh; fi
    if (( now > before )); then
      echo "FAIL $tool $f: $before -> $now findings"
      echo "$lines" | head -10 | sed 's/^/  /'
      worse=1
    fi
  done
done
(( worse )) || echo "PASS style (${#kt[@]} Kotlin files, no new ktlint/detekt findings)"
exit $worse
