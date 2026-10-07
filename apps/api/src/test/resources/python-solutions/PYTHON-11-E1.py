from unittest.mock import Mock


def pay(gateway, amount: int) -> bool:
    try:
        return gateway.charge(amount) == "ok"
    except TimeoutError:
        return False


gateway = Mock()
gateway.charge.return_value = "ok"
print(f"paiement accepté: {pay(gateway, 120)}")

gateway.charge.assert_called_once_with(120)
print("appel vérifié: charge(120) une seule fois")

slow = Mock()
slow.charge.side_effect = TimeoutError()
print(f"paiement après timeout: {pay(slow, 120)}")
