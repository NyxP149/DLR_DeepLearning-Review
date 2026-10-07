modules = {"catalogue": {"product"}, "commandes": {"order"}, "identité": {"user"}}
owners = {entity: module for module, entities in modules.items() for entity in entities}
print("Propriétaire order:", owners["order"])
print("Modules:", len(modules))
