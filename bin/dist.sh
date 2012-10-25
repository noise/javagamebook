# zip up the source and related files
find src -name "*.java" -print | zip chap07.zip -@
find bin -name "*.sh" -print | zip chap07.zip -@
find lib -name "LICENSE" -print | zip chap07.zip -@
find lib -name "*.jar" -print | zip chap07.zip -@
find . -name "build.xml" -print | zip chap07.zip -@
find . -name "README" -print | zip chap07.zip -@
