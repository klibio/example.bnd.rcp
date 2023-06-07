# checkout the eclipse repository here

```bash
pushd io.klib.eclipse.snippets/repo
git clone https://github.com/eclipse-platform/eclipse.platform.swt.git
popd
pushd io.klib.eclipse.snippets/src/org/eclipse/swt/
ln -s \
    ../../../../repo/eclipse.platform.swt/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/ \
    snippets
popd
pushd io.klib.eclipse.snippets/src/org/eclipse/swt/snippets/
mv Snippet99.java Snippet99.java_
mv Snippet130.java Snippet130.java_
mv Snippet153.java Snippet153.java_
mv Snippet174.java Snippet174.java_
mv Snippet195.java Snippet195.java_
mv Snippet209.java Snippet209.java_
mv Snippet341.java Snippet341.java_
mv Snippet378.java Snippet378.java_
mv Snippet379.java Snippet379.java_
mv Snippet380.java Snippet380.java_
popd
```
