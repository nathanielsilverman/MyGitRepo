#
Document Database with Search engine: 

Developed with maven dependency manager.
  Dependencies used:
    - apache.pdfbox
    - google.code.gson
    - JUnit4

Stage1: Document Database in RAM:
    - Stores documents in self-made implmentation of a HashTable that uses separate chaining to deal with collisions. Runs O(n) time. 
    - Basic design allows adding and removing documents both txt and pdfs into database using a documents URI.
    
Stage2: Added Undo Support:
    - Utilizes a self-made implmentation of a Stack to undo the last action made to the document using lambdas.
    
Stage3: Added Search Engine:
    - Utilizes a self-made implmentation of a Trie to search documents for specific keywords.
    
Stage4: Added Memory Management Part 1:
    - Utilizes a self-made implmentation of a MinHeap to keep track of recently used documents, deleting references to all the least recently-used documents from memory if the memory limit is exceeded.
    
Stage5: Added Memory Managment Part 2:
    - Utilizes a self-made implmentation of a BTree to store documents, when limit is reached the least recently-used documents are moved between RAM and the computers drive using java json serialization/deserialization.
