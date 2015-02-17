PATH=~/bin:$PATH

BRANCH=$(git symbolic-ref HEAD | xargs echo -n | perl -p -e 's/refs\/heads\//')

case $BRANCH in
master)
	VSFX=""
	;;
*/[A-Z]*-[0-9]*-*)
	VSFX=$(echo -n $BRANCH | perl -p -e 's/^.*?([A-Z]+-[0-9]+)-.*$/-$1/')
	;;
*)
	VSFX=$(echo -n "-${BRANCH}" | perl -p -e 's/\W/-/g')
	;;
esac

VERSION=$(mvn -q \
     -Dexec.executable="echo" \
     -Dexec.args='${project.version}' \
     --non-recursive \
     org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | xargs echo -n)

NEW_VERSION=$(echo $VERSION | perl -p -e "s/-SNAPSHOT/${VSFX}-SNAPSHOT/")

mvn versions:set versions:commit -DnewVersion=${NEW_VERSION}

