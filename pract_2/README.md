# Визуализатор зависимостей (Вариант 27)

Визуализатор зависимостей пакетов NuGet, написанный на языке Java.

## Поддерживаемые аргументы

* `-package-name` - название анализируемого пакета в NuGet Gallery
* `-package-url` - URL-адрес анализируемого пакета
* `-package-file` - путь к файлу анализируемого пакета
* `-dotnet-version` - рассматривая версия .NET (по умолчанию: net10.0)
* `-depth` - глубина исследования (по умолчанию: 5)
* `-fake-nuget` - путь к тестовому источнику
* `-print-tree` - вывод ASCII дерева в консоль (по умолчанию: false)
* `-build-diagram` - вывод диаграмы на языке PlantUML (по умолчанию: true)

## Пример использования - граф для Polly

```text
-depth 10 -package-name Polly -dotnet-version .NETFramework4.6.2 -print-tree -build-diagram
```

Получаем следующий результат:

```text
Downloading root from NuGet Gallery
Getting info about dependencies...
Polly (8.9.1)
> Polly.Core (8.9.1)
> > Microsoft.Bcl.AsyncInterfaces (11.0.0)
> > > System.Threading.Tasks.Extensions (4.6.3)
> > > > System.Runtime.CompilerServices.Unsafe (6.1.2)
> > Microsoft.Bcl.TimeProvider (11.0.0)
> > > Microsoft.Bcl.AsyncInterfaces (11.0.0)
> > > System.ValueTuple (4.6.2)
> > System.ComponentModel.Annotations (5.0.0)
> > System.Threading.Tasks.Extensions (4.6.3)
> > System.ValueTuple (4.6.2)
@startuml
[Polly] --> [Polly.Core]
[Polly.Core] --> [Microsoft.Bcl.AsyncInterfaces]
[Microsoft.Bcl.AsyncInterfaces] --> [System.Threading.Tasks.Extensions]
[System.Threading.Tasks.Extensions] --> [System.Runtime.CompilerServices.Unsafe]
[Polly.Core] --> [Microsoft.Bcl.TimeProvider]
[Microsoft.Bcl.TimeProvider] --> [Microsoft.Bcl.AsyncInterfaces]
[Microsoft.Bcl.TimeProvider] --> [System.ValueTuple]
[Polly.Core] --> [System.ComponentModel.Annotations]
[Polly.Core] --> [System.Threading.Tasks.Extensions]
[Polly.Core] --> [System.ValueTuple]
@enduml
```

## Пример использования - граф с тестовым источником

```text
-fake-nuget test/fake.txt -package-name TRASH -print-tree false
```

Получаем следующий результат:

```text
@startuml
[TRASH] --> [GARBAGE]
[GARBAGE] --> [NICEFRAMEWORK]
[NICEFRAMEWORK] --> [LEFTPAD]
[LEFTPAD] --> [OPENAI]
[OPENAI] --> [HTTPCORE]
[LEFTPAD] --> [CEF]
[CEF] --> [BLINK]
[CEF] --> [V8]
[NICEFRAMEWORK] --> [MADEBYONEGUY]
[MADEBYONEGUY] --> [WEBSERVER]
[WEBSERVER] --> [HTTPCORE]
[WEBSERVER] --> [TLSCORE]
[MADEBYONEGUY] --> [REGEX]
[NICEFRAMEWORK] --> [SOMETHINGELSE]
[GARBAGE] --> [WEBSERVER]
[GARBAGE] --> [MADEBYONEGUY]
[GARBAGE] --> [CEF]
[GARBAGE] --> [TRASH]
[TRASH] --> [CEF]
[TRASH] --> [OPENAI]
@enduml
```
