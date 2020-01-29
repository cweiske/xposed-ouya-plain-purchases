********************
OUYA plain purchases
********************

Xposed__ module that deactivates encryption on the OUYA Android gaming console.

Its goal is that purchase requests get sent plain-text to the
`discover store server`__,
and the server may reply with plain text purchase receipts.

Works by patching ``javax.crypto.Cipher::doFinal(byte[])`` to return
the input without encryption/decryption.

__ https://repo.xposed.info/module/de.robv.android.xposed.installer
__ http://cweiske.de/ouya-store-api-docs.htm


Development hints
=================
- Version number can be changed in ``xposed-ouya-plain-purchases/build.gradle``
