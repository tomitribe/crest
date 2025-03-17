#!/bin/bash


function _propose_files() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  COMPREPLY=($(compgen -f "$cur"))
}


function _propose_flags() {
  local FLAGS="$@"
  local cur=${COMP_WORDS[COMP_CWORD]}

  # minus flags we've used
  for ((i = 0; i < ${#COMP_WORDS[*]} - 1; i++)); do
    n="${COMP_WORDS[$i]}"
    [[ "$n" == -* ]] && {
      n="${n/=*/=}"
      FLAGS=("${FLAGS[@]/$n/}")
    }
  done

  COMPREPLY=($(compgen -W "${FLAGS[*]}" -- "$cur"))
}


function _propose_flag_values() {
  local VALUES="$@"
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -W "${VALUES[*]}" "$cur"))
}


function _propose_flag_file_values() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  cur="$(echo "$cur" | perl -pe 's/[^=]+=//')"
  COMPREPLY=($(compgen -f "$cur"))
}

function _enums() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  local args_length=${#COMP_WORDS[@]}

  local COMMANDS=(
    red
    help
    green
    blue
  )

  # List the commands
  [ $args_length -lt 3 ] && {
    COMPREPLY=($(compgen -W "${COMMANDS[*]}" "$cur"))
    return
  }

  # Command chosen.  Delegate to its completion function

  # Verify the command is one we know and execute the
  # function that performs its completion
  local CMD=${COMP_WORDS[1]}
  for n in "${COMMANDS[@]}"; do
    [ "$CMD" = "$n" ] && {
      CMD="$(echo "$CMD" | perl -pe 's,[^a-zA-Z0-9],,g')"
      _enums_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _enums_red() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --time=*) _propose_flag_values "NANOSECONDS" "MICROSECONDS" "MILLISECONDS" "SECONDS" "MINUTES" "HOURS" "DAYS" ;;
  -*) _propose_flags "--time=";;
  *) _propose_files ;;
  esac

}

function _enums_help() {
  _propose_files
}

function _enums_green() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --time=*) _propose_flag_values "NANOSECONDS" "MICROSECONDS" "MILLISECONDS" "SECONDS" "MINUTES" "HOURS" "DAYS" ;;
  --shape=*) _propose_flag_values "CIRCLE" "SQUARE" "TRIANGLE" ;;
  -*) _propose_flags "--time=" "--shape=";;
  *) _propose_files ;;
  esac

}

function _enums_blue() {
  _propose_files
}

complete -F _enums enums
