values = [3, "dlr", 2.5, [1], None]
print("Types: " + ", ".join(type(value).__name__ for value in values))
print("Somme des cubes impairs:", sum(n ** 3 for n in range(1, 7) if n % 2 == 1))

config = {"a": 1}
alias = config
copy = dict(config)
alias["b"] = 2
print("Alias:", config, alias)
print("Copie:", copy, config)
