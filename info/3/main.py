import re

def task0(string):
    return len(re.findall(r":-O", string))

def task1(string):
    lines = re.split(r"/", string)
    if len(lines) != 3:
        return "Не хайку. Должно быть 3 строки."

    vowels_count = []
    for line in lines:
        line = line.lower()
        vowels = re.findall(r"[ауоыэяюёие]", line)
        vowels_count.append(len(vowels))

    if vowels_count == [5, 7, 5]:
        return "Хайку!"
    else:
        return "Не хайку."

def task2(string):
    words = re.findall(r"[А-яёЁ]+", string)

    result = []
    for word in words:
        match = re.fullmatch(r"[^ауоыэяюёие]*([ауоыэяюёие])(\1|[^ауоыэяюёие])*", word, flags=re.IGNORECASE)
        if match:
            result.append(match.group())
    result.sort(key=len)

    return result

def test(task, tests):
    print(f"Testing {task.__name__}:")
    FMT_STR = "  {}: ({}, {}) => {} [{}]"

    for i, (x, y) in enumerate(tests):
        res = task(x)
        print(FMT_STR.format(i+1, x, y, res, "PASS" if y == res else "FAIL"))

TESTS_TASK0 = [
    ("", 0), (":-O", 1),
    ("::-O", 1), (":--O", 0), (":-OO", 1),
    (":-:-O", 1), (":-O:-OO", 2),
    ("Lorem :-O i:-psum d:-olor sit:-O amet", 2),
]

TESTS_TASK1 = [
    ("Вечер за окном. / Еще один день прожит. / Жизнь скоротечна...",
     "Хайку!"),
    ("Просто текст", "Не хайку. Должно быть 3 строки."),
    ("Как вишня расцвела! / Она с коня согнала / И князя-гордеца.",
     "Не хайку."),
    ("", "Не хайку. Должно быть 3 строки."),
    (" / / / ", "Не хайку. Должно быть 3 строки."),
    (" / / ", "Не хайку.")
]

TESTS_TASK2 = [
    ("Классное слово – обороноспособность, " +
     "которое должно идти после слов: трава " +
     "и молоко.",
     ["и", "слов", "идти", "слово", "трава",
      "должно", "молоко", "обороноспособность"]
    ),
    ("", []),
    ("Для простоты будем считать",
     ["Для"]),
    ("Необходимо выбрать три любых буквы и расстояние между ними",
     ["и", "три", "ними"]),
    ("через один друг от друга",
     ["от", "друг", "через"])
]

test(task0, TESTS_TASK0)
test(task1, TESTS_TASK1)
test(task2, TESTS_TASK2)

tasks = [task0, task1, task2]
while True:
    task_id = int(input())
    test_str = input()

    print(tasks[task_id](test_str))

