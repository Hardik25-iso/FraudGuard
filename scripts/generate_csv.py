import random
from datetime import datetime, timedelta

# Known accounts from data.sql
# ACC1017 has 0 balance (triggers SuspiciousActivityRule on deposit)
# ACC1001-ACC1020 are standard
account_ids = [f'ACC{i}' for i in range(1001, 2650)]

def generate_diverse_csv(file_path, count=500):
    with open(file_path, 'w') as f:
        f.write('transactionId,transactionType,amount,transactionTime,sourceAccountId,destinationAccountId\n')
        
        base_time = datetime(2026, 4, 16, 10, 0, 0)
        
        for i in range(1, count + 1):
            txn_id = f'TXN-BULK-{i:04d}'
            
            # Scenario selection
            dice = random.random()
            
            if dice < 0.10: # HIGH RISK Scenario: Large Amount + Odd Hours
                txn_type = random.choice(['DEPOSIT', 'TRANSFER'])
                amount = round(random.uniform(500000.0, 1000000.0), 2)
                txn_time = datetime(2026, 4, 17, random.randint(0, 3), random.randint(0, 59))
                source_acc = random.choice(account_ids)
                dest_acc = random.choice(account_ids)
                
            elif dice < 0.20: # MEDIUM RISK Scenario: Medium Amount + Odd Hours
                txn_type = random.choice(['WITHDRAWAL', 'TRANSFER'])
                amount = round(random.uniform(60000.0, 150000.0), 2)
                txn_time = datetime(2026, 4, 17, random.randint(1, 4), random.randint(0, 59))
                source_acc = random.choice(account_ids)
                dest_acc = random.choice(account_ids)
                
            elif dice < 0.30: # RAPID FIRE Scenario
                # Cluster 3 transactions in the same minute
                source_acc = 'ACC1005'
                for j in range(3):
                    sub_txn_id = f'TXN-BULK-{i:04d}-{j}'
                    txn_type = 'TRANSFER'
                    amount = round(random.uniform(1000.0, 5000.0), 2)
                    txn_time = base_time + timedelta(seconds=j*10)
                    time_str = txn_time.strftime('%Y-%m-%dT%H:%M:%S')
                    dest_acc = random.choice(account_ids)
                    while dest_acc == source_acc:
                        dest_acc = random.choice(account_ids)
                    f.write(f"{sub_txn_id},TRANSFER,{amount},{time_str},{source_acc},{dest_acc}\n")
                # Skip the normal write at bottom
                base_time += timedelta(minutes=random.randint(1, 5))
                continue
                
            elif dice < 0.35: # SUSPICIOUS ACTIVITY Scenario: Large deposit on zero-balance account
                txn_type = 'DEPOSIT'
                amount = round(random.uniform(20000.0, 50000.0), 2)
                txn_time = base_time
                source_acc = None
                dest_acc = 'ACC1017' # ACC1017 has 0 balance in data.sql
                
            else: # NORMAL/LOW RISK Scenario
                txn_type = random.choice(['DEPOSIT', 'WITHDRAWAL', 'TRANSFER'])
                amount = round(random.uniform(100.0, 15000.0), 2)
                txn_time = base_time
                source_acc = random.choice(account_ids)
                dest_acc = random.choice(account_ids)
                # Sometimes add odd hours to normal txns for low alerts
                if random.random() < 0.1:
                    txn_time = datetime(2026, 4, 17, 2, random.randint(0, 59))

            time_str = txn_time.strftime('%Y-%m-%dT%H:%M:%S')
            
            if txn_type == 'DEPOSIT':
                f.write(f"{txn_id},DEPOSIT,{amount},{time_str},,{dest_acc}\n")
            elif txn_type == 'WITHDRAWAL':
                f.write(f"{txn_id},WITHDRAWAL,{amount},{time_str},{source_acc},\n")
            else: # TRANSFER
                if not source_acc: source_acc = random.choice(account_ids)
                while dest_acc == source_acc:
                    dest_acc = random.choice(account_ids)
                f.write(f"{txn_id},TRANSFER,{amount},{time_str},{source_acc},{dest_acc}\n")
            
            # Advance base time slightly for normal progression
            base_time += timedelta(minutes=random.randint(1, 5))

    print(f"Generated diverse sample-bulk-transactions.csv with {count} records.")

if __name__ == "__main__":
    generate_diverse_csv('sample-bulk-transactions.csv')
