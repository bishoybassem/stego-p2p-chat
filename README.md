#Stego-P2P Chat

A Java chat application implemented in a decentralized P2P fashion. It uses several cryptographic algorithms and image steganography (used in [Stego-Hide](https://github.com/bishoybassem/stego-hide)) to secure chat messages.

### Message Securing

Cryptographic algorithms are used for securing the user's chat messages before being embedded in the image. Chat messages are encrypted according to the following scheme:

formatedMessage = Flag || IP<sub>R</sub> || Name<sub>R</sub> || IP<sub>S</sub> || Name<sub>S</sub> || ChatMessage <br>
encryptedMessage = E<sub>AES, K</sub>(formatedMessage || Sign<sub>PR<sub>S</sub></sub>(formatedMessage))

Where E<sub>AES, K</sub> is the AES encryption using a 128 bits symmetric key K, Sign<sub>PR<sub>S</sub></sub> is the digital signature algorithm (DSA) for generating signatures signed with the senders private key, IP<sub>S</sub> and IP<sub>R</sub> are the ip addresses of the sender and the receiver respectively, Name<sub>S</sub> and Name<sub>R</sub> are the used names of the sender and the receiver respectively, and Flag is a bit flag (0/1) denoting whether the sent message is public or private.

The user's identity consists of his public data, which are his used name and his public DSA key. This identity message acts as a log-in message, therefore, it is also secured to prevent any unauthorized access. It is encrypted according to the following scheme:

encryptedIdentity = E<sub>AES, K</sub>(IP<sub>S</sub> || Name<sub>S</sub> || PU<sub>S</sub> || Sign<sub>PR<sub>S</sub></sub>(IP<sub>S</sub> || Name<sub>S</sub> || PU<sub>S</sub>))


### Security Features

* Message Confidentiality : is assured through using a pseudo random generator to randomize the hide locations inside the cover image. Also, it is assured by securing the chat messages using the AES encryption before embedding. This way, only the application users are able to extract the encrypted message from the received image and decrypt it.
* Message Integrity, Authentication & Non-repudiation : are assured through the use of digital signatures. Only the sender's public key can decrypt the signature, thus, it was encrypted using the sender's private key, which provides authentication and non-repudiation. If the digest of the message matches the decrypted one from the signature, then integrity is also assured (i.e. the message has not been tampered with).
* User Authentication : is assured by securing the user's broadcasted identity. The AES encryption with a specific symmetric key allows the authentication of only the parties having this key (i.e. the application users). Moreover, verifying the received signature using the identity's public key assures it's integrity. 

### Download

[Version 1.0](https://github.com/bishoybassem/stego-p2p-chat/releases/download/v1.0/Stego-P2P.Chat.jar)

### Screenshots

![screen1](/screenshots/screen1.jpg)

![screen2](/screenshots/screen2.jpg)

![screen3](/screenshots/screen3.jpg)