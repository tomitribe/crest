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

function _foo() {
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
      _foo_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _foo_red() {
  _propose_files
}

function _foo_help() {
  _propose_files
}

function _foo_green() {
  _propose_files
}

function _foo_blue() {
  _propose_files
}

complete -F _foo foo
