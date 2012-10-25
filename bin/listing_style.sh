# use astyle to format code listings for the book
# 4 spaces per tab
# java style
# pad around operators
#
astyle -j -s4 -p -a --brackets=break-closing-headers --convert-tabs < $1 > listings/$2
#astyle -j -s4 -p --convert-tabs --style=java < $1 > listings/$2

