# Rules for shrinking the release build (R8).
# Sigma doesn't use reflection, so the default Android rules are enough.
# Keep line numbers so crash reports in Play Console point to the right line.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
