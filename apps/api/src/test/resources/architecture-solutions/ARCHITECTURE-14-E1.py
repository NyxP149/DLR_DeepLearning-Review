audit = []
def approve(role: str, order_id: int) -> str:
    if role != "manager":
        audit.append((order_id, "DENIED"))
        return "403"
    audit.append((order_id, "APPROVED"))
    return "200"
print("Employé:", approve("employee", 42))
print("Manager:", approve("manager", 42))
print("Audit:", audit)
