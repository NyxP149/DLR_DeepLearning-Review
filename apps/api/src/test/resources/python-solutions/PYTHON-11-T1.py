from unittest.mock import Mock


def notify(client, address):
    try:
        client.send(address, "Bienvenue")
    except TimeoutError:
        return False
    return True


client = Mock()
print("notification envoyee:", notify(client, "ada@dlr.dev"))
client.send.assert_called_once_with("ada@dlr.dev", "Bienvenue")
print("appel verifie: send('ada@dlr.dev', 'Bienvenue') une seule fois")

slow = Mock()
slow.send.side_effect = TimeoutError()
print("notification apres timeout:", notify(slow, "ada@dlr.dev"))
