import os
import re
from pathlib import Path

src_dir = Path('src/main/java')
test_dir = Path('src/test/java')

mapping = {
    'com.fraudguard.service.file': 'com.fraudguard.swaraj.file',
    'com.fraudguard.service.profile': 'com.fraudguard.hardik.profile',
    'com.fraudguard.service.rule': 'com.fraudguard.tejas.rule',
    'com.fraudguard.service.risk': 'com.fraudguard.tejas.risk',
    'com.fraudguard.service.detection': 'com.fraudguard.tejas.detection',
    'com.fraudguard.service.account': 'com.fraudguard.deep.service.account',
    'com.fraudguard.service.analysis': 'com.fraudguard.deep.service.analysis',
    'com.fraudguard.service.logging': 'com.fraudguard.deep.service.logging',
    'com.fraudguard.service': 'com.fraudguard.deep.service',
    'com.fraudguard.model': 'com.fraudguard.hardik.model',
    'com.fraudguard.exception': 'com.fraudguard.hardik.exception',
    'com.fraudguard.dto': 'com.fraudguard.deep.dto',
    'com.fraudguard.controller': 'com.fraudguard.deep.controller',
    'com.fraudguard.repository': 'com.fraudguard.deep.repository',
    'com.fraudguard.config': 'com.fraudguard.deep.config',
}

sorted_keys = sorted(mapping.keys(), key=len, reverse=True)

def process_file(file_path, base_dir):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find current package
    pkg_match = re.search(r'^package\s+(com\.fraudguard.*?);', content, re.MULTILINE)
    if not pkg_match:
        return

    old_pkg = pkg_match.group(1)
    new_pkg = old_pkg
    
    # Calculate new package
    for k in sorted_keys:
        if old_pkg == k or old_pkg.startswith(k + '.'):
            new_pkg = old_pkg.replace(k, mapping[k], 1)
            break

    # If it's the main application or its tests, we keep it in com.fraudguard
    if file_path.name in ['FraudGuardApplication.java', 'FraudGuardApplicationTests.java']:
        new_pkg = 'com.fraudguard'

    # Global text replacement for all mapping keys
    for k in sorted_keys:
        content = re.sub(rf'(?<=[^\w]){re.escape(k)}(?=[.;])', mapping[k], content)
        content = re.sub(rf'^{re.escape(k)}(?=[.;])', mapping[k], content, flags=re.MULTILINE)

    new_rel_path = new_pkg.replace('.', '/') + '/' + file_path.name
    new_file_path = base_dir / new_rel_path

    new_file_path.parent.mkdir(parents=True, exist_ok=True)
    
    with open(new_file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    if new_file_path != file_path:
        os.remove(file_path)

for root_dir in [src_dir, test_dir]:
    if root_dir.exists():
        java_files = list(root_dir.rglob('*.java'))
        for f in java_files:
            process_file(f, root_dir)

def remove_empty_dirs(path):
    if not path.is_dir():
        return
    for p in path.iterdir():
        if p.is_dir():
            remove_empty_dirs(p)
    if not list(path.iterdir()):
        try:
            path.rmdir()
        except OSError:
            pass

remove_empty_dirs(src_dir)
remove_empty_dirs(test_dir)

print("Refactoring complete.")
