Submission Tool
============

What It Does
------------
###Encryption
First, the application zips all the requested files. Next, it
generates a 128-bit AES key and encrypts the zip file with it.

The AES key is then itself encrypted via the provided public
key.

###Decryption
In decryption, the application decrypts the provided encrypted 
AES key by using the provided private key, then uses that AES
key to decrypt the provided file. (Probably a zip file from
step 1)

###Bundling
Finally, the saved, encrypted AES key and the encrypted zip file
are both zipped again into a final zip file.

###Submitting
This final zip file can then be submitted to a github repository
using the `Submit` class.

How To Use
----------
###RSA Keys
You'll need a public/private key pair in binary format. Run:
`openssl genrsa -out private.pem 2048` to get your private key,
then `openssl pkcs8 -topk8 -in private.pem -outform DER -out private.der -nocrypt`
to get it into the DER format.

Finally, `openssl rsa -in private.pem -pubout -outform DER -out public.der`
for the public key.

###Running

####Encryption
`java Bundler public_key.der MyFile.java MyOtherFile Etc`
####Decryption
`java Bundler -d private_key.der encrypted.zip encryped-aes.der`
####Submitting
`java Submit repoOwner repo submission.zip "commit message"`
####All in one
`ant submit`

(note, ant's digesting stdout so my user/pwd prompts aren't getting displayed,
i'm sure it's a simple fix but i'm tired so goodnight)

