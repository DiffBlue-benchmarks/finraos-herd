set -e
STARTLINE=`head -n $2 $1 | grep -n "^  /[*]" | sed "s/:.*//" | tail -n 1`
echo Deleting from line $STARTLINE
sed -i.old "$((STARTLINE-1)),/^  }$/d" $1

