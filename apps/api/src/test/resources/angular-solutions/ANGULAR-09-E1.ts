export {};

type OrderStatus = 'PAID' | 'PENDING' | 'CANCELLED';

interface Order {
  customer: string;
  amount: number;
  status: OrderStatus;
}

const orders: Order[] = [
  { customer: 'Ada', amount: 120, status: 'PAID' },
  { customer: 'Linus', amount: 60, status: 'PAID' },
  { customer: 'Ada', amount: 80, status: 'PAID' },
  { customer: 'Grace', amount: 200, status: 'PENDING' },
  { customer: 'Linus', amount: 40, status: 'CANCELLED' },
];

function paid(list: Order[]): Order[] {
  return list.filter((order) => order.status === 'PAID');
}

function revenue(list: Order[]): number {
  return paid(list).reduce((sum, order) => sum + order.amount, 0);
}

function averageBasket(list: Order[]): number {
  const payments = paid(list);
  return payments.length === 0 ? 0 : Math.round(revenue(list) / payments.length);
}

function countByStatus(list: Order[]): Record<OrderStatus, number> {
  const counts: Record<OrderStatus, number> = { PAID: 0, PENDING: 0, CANCELLED: 0 };
  for (const order of list) {
    counts[order.status]++;
  }
  return counts;
}

function bestCustomer(list: Order[]): [string, number] {
  const totals = new Map<string, number>();
  for (const order of paid(list)) {
    totals.set(order.customer, (totals.get(order.customer) ?? 0) + order.amount);
  }
  let best: [string, number] = ['', 0];
  for (const entry of totals) {
    if (entry[1] > best[1]) {
      best = entry;
    }
  }
  return best;
}

const counts = countByStatus(orders);
const [customer, amount] = bestCustomer(orders);
console.log(`Chiffre d'affaires: ${revenue(orders)}`);
console.log(`Panier moyen: ${averageBasket(orders)}`);
console.log(`Par statut: PAID=${counts.PAID}, PENDING=${counts.PENDING}, CANCELLED=${counts.CANCELLED}`);
console.log(`Meilleur client: ${customer} (${amount})`);
