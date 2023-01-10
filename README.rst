********************
OUYA plain purchases
********************

Xposed__ module that deactivates encryption on the OUYA Android gaming console.

Its goal is that purchase requests get sent plain-text to the
`discover store server`__,
and the server may reply with plain text purchase receipts.

Works by patching ``javax.crypto.Cipher::doFinal(byte[])`` to return
the input without encryption/decryption.

It needs to be `installed as system application`__ to prevent
Final Fantasy III from crashing.

__ https://repo.xposed.info/module/de.robv.android.xposed.installer
__ http://cweiske.de/ouya-store-api-docs.htm
__ http://cweiske.de/tagebuch/ouya-final-fantasy3.htm#howto


Development hints
=================
- Version number can be changed in ``xposed-ouya-plain-purchases/build.gradle``
