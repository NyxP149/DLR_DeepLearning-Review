export {};

type Shape =
  | { kind: 'circle'; radius: number }
  | { kind: 'rectangle'; width: number; height: number }
  | { kind: 'square'; side: number };

function assertNever(value: never): never {
  throw new Error(`cas non géré: ${JSON.stringify(value)}`);
}

function area(shape: Shape): number {
  switch (shape.kind) {
    case 'circle':
      return Math.PI * shape.radius ** 2;
    case 'rectangle':
      return shape.width * shape.height;
    case 'square':
      return shape.side ** 2;
    default:
      return assertNever(shape);
  }
}

const labels: Record<Shape['kind'], string> = { circle: 'cercle', rectangle: 'rectangle', square: 'carré' };
const shapes: Shape[] = [
  { kind: 'circle', radius: 5 },
  { kind: 'rectangle', width: 3, height: 4 },
  { kind: 'square', side: 2 },
];
for (const shape of shapes) {
  console.log(`${labels[shape.kind]}: ${area(shape).toFixed(2)}`);
}
