def describe_language(name: str) -> str:
    return f"{name}: dynamique et lisible"


def type_names(values: list) -> str:
    return ", ".join(type(value).__name__ for value in values)


def sum_even_squares(numbers: list[int]) -> int:
    return sum(n * n for n in numbers if n % 2 == 0)


def double(value: int) -> int:
    return value * 2


print(describe_language("Python"))
print("Types:", type_names([3, "a", [1], 2.5]))
print("Somme des carres pairs:", sum_even_squares([1, 2, 3, 4, 5]))
print("Annotation non imposee:", double("a"))
a = [1, 2]
b = a
c = a.copy()
b.append(3)
print("Alias:", a, b)
print("Copie:", a, c)
