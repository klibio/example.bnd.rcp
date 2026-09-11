# XSLT transform utility

This folder contains a small Java command-line utility for editing Eclipse p2 repository metadata stored in a JAR. The utility applies an XSLT stylesheet to `content.xml` or `artifacts.xml`, then writes the transformed XML back into a JAR with the same filename.

## What it does

`io.klib.transform.xslt.XSLTransform` performs this sequence:

1. Reads `transformFile=<path>` from the program arguments.
2. Reads `xsltFile=<path>` when supplied. Without it, loads `xslt/transform.xslt` from the classpath or the current working directory.
3. Creates a temporary directory.
4. If the input is a JAR, extracts only `content.xml` and `artifacts.xml` entries that are present.
5. Renames the XML file matching the JAR name to a `*Source.xml` file, and writes the transformed result to the original XML filename.
6. Deletes the original JAR and source XML, then creates a replacement JAR from the temporary directory.

The replacement JAR is written beside the input and keeps the input base name. The temporary directory is marked for deletion when the JVM exits.

The current implementation only processes JAR input. A non-JAR file passes validation but is not transformed.

## Arguments

Arguments must use `key=value` syntax:

```text
transformFile=<input JAR>
xsltFile=<XSLT file>
```

`transformFile` is required. `xsltFile` is optional and defaults to `xslt/transform.xslt`.

The `backup` system property controls backup creation:

```text
-Dbackup=true
```

When enabled, the original input is copied to `BAK_<input-name>` beside the input before replacement. The copy is not overwritten safely: an existing backup can cause the copy operation to fail.

## Eclipse launch configurations

The `.launch` files run the Java main class `io.klib.transform.xslt.XSLTransform`:

| Launch file | Stylesheet | Backup |
|---|---|---|
| `XSLTransform.launch` | Explicitly uses `xslt/transform.xslt` | No |
| `XSLTransform-external.launch` | Uses the built-in default lookup | Yes, with `-Dbackup=true` |
| `XSLTransform-BACKUP.launch` | Explicitly uses `xslt/transform.xslt` | Yes, with `-Dbackup=true` |

The launch configurations use `data/content.jar` as their sample input. Running one replaces that file. Use a copy of the sample or enable the backup launch configuration when experimenting.

## Stylesheets

All stylesheets use an identity transform: XML nodes and attributes are copied unless a more specific template suppresses a matching node.

### `xslt/transform.xslt`

Removes p2 metadata capability and requirement elements for:

- `required` with `namespace="osgi.service"`
- `requiredProperties` with `namespace="osgi.service"`
- `requiredProperties` with `namespace="osgi.contract"`
- `provided` with `namespace="osgi.service"`
- `provided` with `namespace="osgi.contract"`

### `xslt/transformAllReqCap.xslt`

Removes the same `osgi.service` and `osgi.contract` requirement-property elements and `osgi.service` provided elements. It is a narrower variant of `transform.xslt`.

### `xslt/transformSingleAttribute.xslt`

Removes `requiredProperties` whose `match` attribute is exactly `(objectClass=java.lang.Object)`.

### `xslt/transform_remove_checksums.xslt`

Removes p2 properties named `download.md5`, `download.checksum.md5`, and `download.checksum.sha-256`.

## Direct invocation

After compiling `XSLTransform.java` with its output directory on the classpath, invoke it from this folder, for example:

```powershell
java -cp <classes> io.klib.transform.xslt.XSLTransform `
  "xsltFile=$PWD/xslt/transform_remove_checksums.xslt" `
  "transformFile=$PWD/data/content.jar"
```

For the default stylesheet, omit `xsltFile`. To preserve a copy before replacement:

```powershell
java -Dbackup=true -cp <classes> io.klib.transform.xslt.XSLTransform `
  "transformFile=$PWD/data/content.jar"
```

## Limitations and cautions

- The input JAR is deleted before the replacement JAR is created. Keep an independent copy of important input data.
- Only the two XML filenames listed above are extracted; other JAR entries are discarded when the output JAR is rebuilt.
- The ZIP writer stores extracted files at the archive root and does not preserve original entry metadata.
- Argument parsing splits on every `=`, so paths or values containing `=` are not supported reliably.
- XML transformation errors are printed to standard error/output by the Java exception handlers; the program does not consistently return a non-zero status for every failure.
- The XSLT files are XSLT 1.0 stylesheets and use the JDK's default `TransformerFactory`.