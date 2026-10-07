export {};

interface Observer<T> {
  next: (value: T) => void;
  complete: () => void;
}

class Observable<T> {
  private producer: (observer: Observer<T>) => void;

  constructor(producer: (observer: Observer<T>) => void) {
    this.producer = producer;
  }

  subscribe(observer: Observer<T>): void {
    this.producer(observer);
  }

  filter(predicate: (value: T) => boolean): Observable<T> {
    return new Observable<T>((observer) =>
      this.subscribe({
        next: (value) => {
          if (predicate(value)) {
            observer.next(value);
          }
        },
        complete: () => observer.complete(),
      }),
    );
  }

  map<R>(project: (value: T) => R): Observable<R> {
    return new Observable<R>((observer) =>
      this.subscribe({
        next: (value) => observer.next(project(value)),
        complete: () => observer.complete(),
      }),
    );
  }
}

function of<T>(...values: T[]): Observable<T> {
  return new Observable<T>((observer) => {
    for (const value of values) {
      observer.next(value);
    }
    observer.complete();
  });
}

function logCalls<A extends unknown[], R>(name: string, fn: (...args: A) => R): (...args: A) => R {
  return (...args: A): R => {
    const result = fn(...args);
    console.log(`appel ${name}(${args.join(', ')}) -> ${result}`);
    return result;
  };
}

of(1, 2, 3, 4)
  .filter((value) => value % 2 === 0)
  .map((value) => value * value)
  .subscribe({
    next: (value) => console.log(`valeur: ${value}`),
    complete: () => console.log('terminé'),
  });

const add = logCalls('add', (a: number, b: number) => a + b);
add(1, 2);
