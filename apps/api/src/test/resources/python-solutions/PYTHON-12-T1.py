import asyncio
import time

running = 0
peak = 0


async def fetch(name, semaphore):
    global running, peak
    async with semaphore:
        running += 1
        peak = max(peak, running)
        await asyncio.sleep(0.1)
        running -= 1
        return name


async def main():
    semaphore = asyncio.Semaphore(2)
    started = time.perf_counter()
    results = await asyncio.gather(*(fetch(name, semaphore) for name in "abcd"))
    elapsed = time.perf_counter() - started
    print("Resultats:", results)
    print("Concurrence max:", peak)
    print("Duree sous 0.35 s:", elapsed < 0.35)


asyncio.run(main())
