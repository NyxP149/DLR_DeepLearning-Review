def approximate_tokens(text: str) -> int:
    return len(text.split())

prompt = "Explique la JVM simplement"
print(f"Prompt: {prompt}")
print(f"Tokens approx: {approximate_tokens(prompt)}")
