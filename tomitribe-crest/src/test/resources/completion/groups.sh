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

function _groups() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  local args_length=${#COMP_WORDS[@]}

  local COMMANDS=(
    help
    svn
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
      _groups_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _groups_help() {
  _propose_files
}
function _groups_svn() {
  local cur=${COMP_WORDS[COMP_CWORD]}
  local args_length=${#COMP_WORDS[@]}

  local COMMANDS=(
    checkout
    commit
  )

  # List the commands
  [ $args_length -lt 4 ] && {
    COMPREPLY=($(compgen -W "${COMMANDS[*]}" "$cur"))
    return
  }

  # Command chosen.  Delegate to its completion function

  # Verify the command is one we know and execute the
  # function that performs its completion
  local CMD=${COMP_WORDS[2]}
  for n in "${COMMANDS[@]}"; do
    [ "$CMD" = "$n" ] && {
      CMD="$(echo "$CMD" | perl -pe 's,[^a-zA-Z0-9],,g')"
      _groups_svn_$CMD
      return
    }
  done

  COMPREPLY=()
}


function _groups_svn_checkout() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --password=*) _propose_flag_file_values ;;
  --username=*) _propose_flag_file_values ;;
  -*) _propose_flags "--password=" "--username=";;
  *) _propose_files ;;
  esac

}

function _groups_svn_commit() {
  local cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
  --message=*) _propose_flag_file_values ;;
  --password=*) _propose_flag_file_values ;;
  -*) _propose_flags "--message=" "--password=";;
  *) _propose_files ;;
  esac

}

complete -F _groups groups
