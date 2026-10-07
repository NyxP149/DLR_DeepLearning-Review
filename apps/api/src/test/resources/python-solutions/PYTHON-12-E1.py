import asyncio
import time


async def fetch(name: str, delay: float) -> str:
    await asyncio.sleep(delay)
    return name


async def main() -> None:
    start = time.perf_counter()
    results = await asyncio.gather(fetch("a", 0.1), fetch("b", 0.1), fetch("c", 0.1))
    elapsed = time.perf_counter() - start
    print(f"Résultats: {results}")
    print(f"Concurrent: {elapsed < 0.25}")
    try:
        await asyncio.wait_for(fetch("lent", 0.5), timeout=0.05)
    except asyncio.TimeoutError:
        print("Timeout: délai dépassé")


asyncio.run(main())
