# Эмулятор коммандной строки (Вариант 27)

Эмулятор коммандной строки с консольным интерфейсом (CLI), написанный на языке
Java. Виртуальная файловая система загружается из директории.

## Поддерживаемые команды

* `help` - вывод информации о командах
* `cd [directory]` - вход в директорию
* `ls [directory]` - вывод содержания директории
* `cat <file>` - вывод содержания файла
* `touch <file>` - создание пустого файла
* `mkdir <directory>` - создание пустой директории
* `rm <file>` - удаление файла
* `rmdir <directory>` - удаление директории
* `cp <from_file> <to_file>` - копирование файла
* `date` - вывод даты и времени
* `cal` - вывод календаря
* `noted <file>` - простой текстовый редактор
* `test [arg]...` - проверочная команда, которя выводит своё имя и аргументы
* `exit` - выход из эмулятора

## Пример использования

Пример использования эмулятора, где создаётся текстовый файл:

```text
multiple_levels /$ touch my_file.txt
multiple_levels /$ noted my_file.txt
> write
Roses are red, 
> writeln
Violets are blue,
> writeln
i forgot
> save
> done
multiple_levels /$ cat my_file.txt
Roses are red, Violets are blue,
i forgot
multiple_levels /$
```

