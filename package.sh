dir=$1
mkdir $dir
cp -r dist/* $dir
ant -Dconfig.file=$2
cp Submit.jar $dir/lib/ 
cp $3 $dir/tests/
shift 3
cp $@ $dir/
zip -r "$dir.zip" $dir
