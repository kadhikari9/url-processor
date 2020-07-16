REPO_DIR=/home/kadhikari/Downloads/Coding_Assignment/inputData/
SPLITTED_DIR=/home/kadhikari/Downloads/Coding_Assignment/inputData/splitted/
inotifywait -m $REPO_DIR -e create -e moved_to |
    while read dir action file; do
        echo "New file detected ['$file']"
        gunzip -c $file | split -l 2500 -d --additional-suffix=.txt - $SPLITTED_DIR/$file
    done
