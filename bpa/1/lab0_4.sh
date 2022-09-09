# 1
wc -l `grep -r "" lab0 2>&1 | grep -o ".*:" | grep -o "[/_0-9a-zA-Z]*" | grep "/m[_0-9a-zA-Z]*$"` | sort

# 2
ls -lc `grep -r "" lab0 2> /dev/null | grep -o ".*:" | grep -o "[/_0-9a-zA-Z]*" | grep "/m[_0-9a-zA-Z]*$"` | head -n 4

# 3
# there is not files like this, but cmd is:
# cat -n `grep -r "" lab0 2>&1 | grep -o ".*:" | grep -o "[/_0-9a-zA-Z]*" | grep "/[_0-9a-zA-Z]*6$" | sort -r`

# 4
ls lab0/mightyena8 | sort -r

# 5
ls -lcr lab0/metagross6 2>/tmp/some_error_file
rm /tmp/some_error_file

# 6
ls -l `grep -r "" lab0/starmie6 | grep -o ".*:" | grep -o "[/_0-9a-zA-Z]*" 2>/dev/null` | sort -n -k 2 -r
