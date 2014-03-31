#### Elias NLP Library

This projects serves as a showcase of some code I wrote back in 2001 for a NLP System codenamed "Elias" or "Talklet Technology".

The code in this project is part of a larger java codebase. It's the core of a high-performance regular expression engine which works with entire tokens instead
of single characters. It allows to embed pattern elements which have grammatical or ontological meaning given corresponding language dictionaries or ontologies.

As such, it is possible to write extremely efficient patterns like these:

'
(What|Who) (is|was) <OBJECT|PERSON>=name [?]
'
where OBJECT or PERSON might be token classes associated with large dictionaries or ontologies. The assignment =name
allows to extract parts of the matched sentences via name.

The engine has been part of a commercial natural language information retrieval and dialogue system which also
contained many more parts like an Ontology Editor / Importer, an IDE, a Web-App and much more.

Given that this system is not commercially viable anymore and that I hold the copyright to the source, I will release parts of it's code as open source under the Apache 2.0 license.

Please keep in mind that this is code from 2001/2002 when many modern java libraries and coding practices didn't exist yet. 

Kai Londenberg, 2014 ( Kai.Londenberg@googlemail.com )
