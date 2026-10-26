from typing import Protocol


class OrderRepository(Protocol):
    def save(self, customer: str, items: list) -> int: ...
    def all_ids(self) -> list: ...


class Notifier(Protocol):
    def notify(self, customer: str) -> None: ...


class MemoryOrders:
    def __init__(self):
        self.orders = {}

    def save(self, customer, items):
        order_id = len(self.orders) + 1
        self.orders[order_id] = (customer, items)
        return order_id

    def all_ids(self):
        return list(self.orders)


class MemoryNotifier:
    def __init__(self):
        self.sent = []

    def notify(self, customer):
        self.sent.append(customer)


class PlaceOrder:
    def __init__(self, repository: OrderRepository, notifier: Notifier):
        self.repository = repository
        self.notifier = notifier

    def __call__(self, customer, items):
        if not items:
            raise ValueError("panier vide")
        order_id = self.repository.save(customer, items)
        self.notifier.notify(customer)
        return order_id


orders = MemoryOrders()
notifier = MemoryNotifier()
place = PlaceOrder(orders, notifier)
print(f"Commande {place('ada', ['stylo'])}: ok (notification a {notifier.sent[0]})")
try:
    place("ada", [])
except ValueError as error:
    print(f"Commande: refusee ({error})")
print("Commandes:", orders.all_ids())
print("Notifications:", len(notifier.sent))
