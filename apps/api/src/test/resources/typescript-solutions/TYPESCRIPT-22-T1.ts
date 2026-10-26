export {};

type Observer<T> = { next: (value: T) => void; complete: () => void };

class Observable<T> {
  constructor(private readonly producer: (observer: Observer<T>) => void) {}

  subscribe(observer: Observer<T>): void {
    this.producer(observer);
  }
}

const source = new Observable<number>((observer) => {
  [1, 2, 3, 4, 5].forEach((value) => observer.next(value));
  observer.complete();
});

function scan<T, A>(reducer: (accumulator: A, value: T) => A, seed: A) {
  return (input: Observable<T>): Observable<A> =>
    new Observable<A>((observer) => {
      let accumulator = seed;
      input.subscribe({
        next: (value) => {
          accumulator = reducer(accumulator, value);
          observer.next(accumulator);
        },
        complete: () => observer.complete(),
      });
    });
}

function take<T>(count: number) {
  return (input: Observable<T>): Observable<T> =>
    new Observable<T>((observer) => {
      let received = 0;
      let done = false;
      input.subscribe({
        next: (value) => {
          if (done) return;
          received += 1;
          observer.next(value);
          if (received >= count) {
            done = true;
            observer.complete();
          }
        },
        complete: () => {
          if (!done) observer.complete();
        },
      });
    });
}

take<number>(3)(scan<number, number>((total, value) => total + value, 0)(source)).subscribe({
  next: (value) => console.log(`cumul: ${value}`),
  complete: () => console.log('termine'),
});
