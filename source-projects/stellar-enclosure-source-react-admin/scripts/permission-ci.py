#!/usr/bin/env python3
"""Bootstrap the fixed SDK adapter; online delivery never uses a floating checkout."""
from pathlib import Path
import json
import os
import re
import subprocess
import sys


def _lock(root):
    path = root / 'permission-toolchain.lock.json'
    if path.is_symlink() or not path.is_file() or path.stat().st_size > 16 * 1024 * 1024:
        raise ValueError('PERMISSION_CI_JSON_INVALID')
    def pairs(items):
        value = {}
        for key, item in items:
            if key in value:
                raise ValueError('PERMISSION_CI_DUPLICATE_KEY')
            value[key] = item
        return value
    value = json.loads(path.read_text(), object_pairs_hook=pairs)
    if not isinstance(value, dict):
        raise ValueError('PERMISSION_CI_JSON_INVALID')
    return value


def _bootstrap_pin(root):
    lock = _lock(root)
    commit = lock.get('sdk', {}).get('commit', '')
    if lock.get('mode') != 'PINNED_COMMIT' or not isinstance(commit, str) or not re.fullmatch(r'(?:[a-f0-9]{40}|[a-f0-9]{64})', commit):
        raise ValueError('PERMISSION_CI_REQUIRES_REAL_COMMITTED_TOOLCHAIN_PIN')
    return commit


def _shared_adapter():
    directory = os.environ.get('PERMISSION_SDK_ROOT') or os.environ.get('PAC_SDK_DIR')
    if not directory:
        raise ValueError('PERMISSION_CI_SDK_ROOT_REQUIRED')
    sdk = Path(directory).resolve()
    path = sdk / 'scripts/permissions/consumer-ci.py'
    if path.is_symlink() or not path.is_file():
        raise ValueError('PERMISSION_CI_SDK_ADAPTER_REQUIRED')
    root = Path(__file__).resolve().parents[1]
    if __name__ == '__main__' or (root / 'permission-toolchain.lock.json').exists():
        if __name__ == '__main__' and len(sys.argv) != 2:
            raise ValueError('PERMISSION_CI_PHASE_REQUIRED')
        lock = _lock(root)
        if __name__ == '__main__' and sys.argv[1] in {'register', 'activate'} and lock.get('mode') != 'PINNED_COMMIT':
            raise ValueError('PERMISSION_CI_LOCAL_TOOLCHAIN_ONLINE_FORBIDDEN')
        if lock.get('mode') == 'PINNED_COMMIT':
            expected = _bootstrap_pin(root)
            head = subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=sdk, capture_output=True, text=True, check=True).stdout.strip()
            status = subprocess.run(['git', 'status', '--porcelain'], cwd=sdk, capture_output=True, text=True, check=True).stdout.strip()
            if head != expected or status:
                raise ValueError('PERMISSION_CI_PINNED_SDK_REQUIRED')
            source = path.read_bytes()
            committed = subprocess.run(['git', 'show', expected + ':scripts/permissions/consumer-ci.py'],
                                       cwd=sdk, capture_output=True, check=True).stdout
            if source != committed:
                raise ValueError('PERMISSION_CI_SDK_ADAPTER_MISMATCH')
            return path, source
    return path, path.read_bytes()


if __name__ == '__main__' and sys.argv[1:] == ['pin']:
    try:
        print(_bootstrap_pin(Path(__file__).resolve().parents[1]))
    except (ValueError, OSError, KeyError, TypeError, AttributeError):
        print('Permission CI input or trust boundary failed; check pinned toolchain', file=sys.stderr)
        sys.exit(2)
else:
    try:
        adapter, source = _shared_adapter()
        exec(compile(source, str(adapter), 'exec'), globals())
    except (ValueError, OSError, KeyError, TypeError, AttributeError, subprocess.CalledProcessError):
        if __name__ != '__main__':
            raise
        print('Permission CI input or trust boundary failed; check pinned SDK and toolchain', file=sys.stderr)
        sys.exit(2)
