What It Does
============

Encryption
----------
First, the application zips all the requested files. Next, it
generates a 128-bit AES key and encrypts the zip file with it.

The AES key is then itself encrypted via the provided public
key.


Decryption
----------
In decryption, the application decrypts the provided encrypted 
AES key by using the provided private key, then uses that AES
key to decrypt the provided file. (Probably a zip file from
step 1)

Next Steps
==========
Zip the encrypted zip and encrypted AES key into a normal
zip and push to github

How To Use
==========
RSA Keys
--------
You'll need a public/private key pair in binary format. Run:
`openssl genrsa -out private.pem 2048` to get your private key,
then `openssl pkcs8 -topk8 -in private.pem -outform DER -out private.der -nocrypt`
to get it into the DER format.

Finally, `openssl rsa -in private.pem -pubout -outform DER -out public.der`
for the public key.

Running
-------

####Encryption
`java Bundler public_key.der MyFile.java MyOtherFile Etc`
####Decryption
`java Bundler -d private_key.der encrypted.zip encryped-aes.der`
