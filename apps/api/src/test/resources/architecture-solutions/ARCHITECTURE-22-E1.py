requests_per_second = 720
capacity_per_instance = 250
instances = -(-requests_per_second // capacity_per_instance)
headroom = instances * capacity_per_instance - requests_per_second
print("Instances:", instances)
print("Marge:", headroom, "req/s")
