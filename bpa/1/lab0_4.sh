wc -l `grep --include="m*" -rl "" lab0` 2>&1 | sort

ls -lc `grep --include="m*" -srl "" lab0` | head -n 4

# cat -n `grep --include="*6" -rl "" lab0` 2>&1 | sort -r

ls -p lab0/mightyena8 | grep -v / | sort -r

ls -lcr lab0/metagross6 2>/tmp/some_error_file
rm /tmp/some_error_file

ls -l `grep --include="*" -srl "" lab0/starmie6` | sort -n -k 2 -r
