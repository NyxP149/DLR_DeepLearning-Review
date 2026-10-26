CONTEXT_WINDOW = 50
RESERVED_OUTPUT = 20
PRICE_IN = 3.0
PRICE_OUT = 15.0


def approximate_tokens(text: str) -> int:
    return (len(text) + 3) // 4


def fits(text: str) -> bool:
    return approximate_tokens(text) <= CONTEXT_WINDOW - RESERVED_OUTPUT


def cost(tokens_in: int, tokens_out: int) -> float:
    return (tokens_in * PRICE_IN + tokens_out * PRICE_OUT) / 1_000_000


prompt = "Explique la JVM simplement"
long_text = "mot " * 40
tokens = approximate_tokens(prompt)
print(f"Prompt: {prompt}")
print(f"Tokens approx: {tokens}")
print(f"Entrée max: {CONTEXT_WINDOW - RESERVED_OUTPUT} tokens")
print(f"Tient: {fits(prompt)}")
print(f"Texte long ({approximate_tokens(long_text)} tokens) tient: {fits(long_text)}")
print(f"Coût estimé: {cost(tokens, 100):.6f} USD")
