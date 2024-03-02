Classic
```
	C -> [C]c
	  -> a
```
"ccc"	unrecognised
"aaa"	unrecognised
"acc"	fully recognised
"accd"	patially recognised

Impossible
```
	A -> [B]
	B -> [C]
	C -> [A]
```
Any input

Simplest indirect
```
	A -> [B]
	B -> b
```

Количество принятых символов резко увеличивается после некоторого шага рекурсии, хотя несколько шагов до этого оно не увеличивается
```
C -> Сc
  -> Cd
  -> e
```
"ecccddd" fully recognised


