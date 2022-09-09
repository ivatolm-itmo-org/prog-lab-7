cat lab0/starmie6/petilil lab0/starmie6/petilil > lab0/gothita3_62

ln -s metagross6 lab0/Copy_21

cp lab0/purrloin1 lab0/metagross6/mantinepurrloin

ln -s ../pidgeot9 lab0/mightyena8/timburrpidgeot

# + permissions
chmod u+w lab0/starmie6/shelmet

cp lab0/pidgeot9 lab0/starmie6/shelmet

# - permissions
chmod u-w lab0/starmie6/shelmet

# + permissions
chmod u+r lab0/mightyena8/drifloon
chmod u+r lab0/mightyena8/dusclops

cp -RP lab0/mightyena8 lab0/tmp
mv lab0/tmp/* lab0/mightyena8/drifloon
rm -rf lab0/tmp

# - permissions
chmod u-r lab0/mightyena8/drifloon
chmod u-r lab0/mightyena8/dusclops

# + permissions
chmod u+w lab0/starmie6

ln lab0/pidgeot9 lab0/starmie6/petililpidgeot

# - permissions
chmod u-w lab0/starmie6
