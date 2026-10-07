export {};

class EventEmitter<T> {
  private listeners: Array<(value: T) => void> = [];

  subscribe(listener: (value: T) => void): void {
    this.listeners.push(listener);
  }

  emit(value: T): void {
    for (const listener of this.listeners) {
      listener(value);
    }
  }
}

class RatingComponent {
  readonly rated = new EventEmitter<number>();
  private value = 0;

  click(stars: number): void {
    if (stars < 1 || stars > 5 || stars === this.value) {
      return;
    }
    this.value = stars;
    this.rated.emit(stars);
  }

  render(): string {
    return `Composant: ${this.value} étoiles`;
  }
}

const rating = new RatingComponent();
rating.rated.subscribe((stars) => console.log(`Parent: note reçue ${stars}`));
rating.click(4);
rating.click(4);
rating.click(9);
rating.click(5);
console.log(rating.render());
