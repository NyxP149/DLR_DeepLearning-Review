import math

PROMPT = "Traduis en anglais : bonjour tout le monde"
LONG_TEXT = "x" * 240
WINDOW = 80
RESERVED = 30


def tokens(text):
    return math.ceil(len(text) / 4)


max_input = WINDOW - RESERVED
cost = tokens(PROMPT) * 2 / 1_000_000 + 100 * 10 / 1_000_000
print(f"Prompt: {PROMPT}")
print(f"Tokens approx: {tokens(PROMPT)}")
print(f"Entree max: {max_input} tokens")
print(f"Tient: {tokens(PROMPT) <= max_input}")
print(f"Texte long ({tokens(LONG_TEXT)} tokens) tient: {tokens(LONG_TEXT) <= max_input}")
print(f"Cout estime: {cost:.6f} USD")
