#!/usr/bin/env python3
"""Actual installer compatibility proof; synthetic temp files and loopback HTTP only.

Set PACKWIZ_INSTALLER_JAR to an existing official v0.5.14 JAR. JAVA_BIN or
JAVA_HOME may select Java. Nothing is installed into a real game instance.
"""
import argparse,hashlib,json,os,pathlib,shutil,subprocess,tempfile,threading,zipfile
from http.server import ThreadingHTTPServer,BaseHTTPRequestHandler
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--keep',action='store_true',help='Keep only the generated synthetic evidence directory')
args=parser.parse_args()
JAR=os.environ.get('PACKWIZ_INSTALLER_JAR')
if not JAR or not pathlib.Path(JAR).is_file():
    parser.error('Set PACKWIZ_INSTALLER_JAR to the official existing v0.5.14 installer JAR')
JAVA=os.environ.get('JAVA_BIN') or (str(pathlib.Path(os.environ['JAVA_HOME'])/'bin/java') if os.environ.get('JAVA_HOME') else shutil.which('java'))
if not JAVA: parser.error('Java is required; configure JAVA_BIN or JAVA_HOME')
with zipfile.ZipFile(JAR) as archive:
    manifest=archive.read('META-INF/MANIFEST.MF').decode()
if 'Implementation-Version: v0.5.14' not in manifest.splitlines():
    parser.error('This acceptance proof is pinned to official installer v0.5.14')
root=pathlib.Path(tempfile.mkdtemp(prefix='ev-packwiz-preserve-proof-'))
sha=lambda data:hashlib.sha256(data).hexdigest()
served={};requests=[]
def add_pack(label,version,tombstone=False,old=False):
    normal=('synthetic-mod-'+version).encode();served['/'+label+'/mods/synthetic.bin']=normal
    text='hash-format = "sha256"\n\n'
    if old or tombstone:
        personal=b'synthetic initial cache'
        text+='[[files]]\nfile = "xaero/a.xwmc"\nhash = "'+sha(personal if old else b'')+'"\nmetafile = false\n'
        if tombstone:text+='preserve = true\nalias = "."\n'
        text+='\n'
        if old:served['/'+label+'/xaero/a.xwmc']=personal
    text+='[[files]]\nfile = "mods/synthetic.bin"\nhash = "'+sha(normal)+'"\nmetafile = false\n'
    index=text.encode();served['/'+label+'/index.toml']=index
    served['/'+label+'/pack.toml']=('name = "Synthetic Preserve Proof"\npack-format = "packwiz:1.1.0"\nversion = "'+version+'"\n[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = "'+sha(index)+'"\n').encode()
class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        requests.append(self.path)
        payload=served.get(self.path)
        self.send_response(200 if payload is not None else 404);self.end_headers()
        if payload is not None:self.wfile.write(payload)
    def log_message(self,*args):pass
server=ThreadingHTTPServer(('127.0.0.1',0),Handler);threading.Thread(target=server.serve_forever,daemon=True).start()
base='http://127.0.0.1:'+str(server.server_port)
add_pack('old','old',old=True);add_pack('transition','2',tombstone=True);add_pack('later','3',tombstone=True);add_pack('unsafe-clean','4')
def duplicate_pack(label,version,retire=False,remove=False):
    binary=('synthetic-shared-'+version).encode()
    binary_uri='/binary/shared-'+version+'.jar'
    served[binary_uri]=binary
    metadata=('name = "Synthetic shared mod"\nfilename = "shared.jar"\nside = "client"\n[download]\nurl = "'+base+binary_uri+'"\nhash-format = "sha256"\nhash = "'+sha(binary)+'"\n').encode()
    served['/'+label+'/mods/primary.pw.toml']=metadata
    served['/'+label+'/mods/.index/duplicate.pw.toml']=metadata
    index='hash-format = "sha256"\n[[files]]\nfile = "mods/primary.pw.toml"\nhash = "'+sha(metadata)+'"\nmetafile = true\n'
    if not remove:
        index+='\n[[files]]\nfile = "mods/.index/duplicate.pw.toml"\n'
        if retire:index+='alias = "."\npreserve = true\nmetafile = false\nhash-format = "sha256"\nhash = "'+sha(b'')+'"\n'
        else:index+='alias = "mods/shared.jar"\nmetafile = true\nhash = "'+sha(metadata)+'"\n'
    index_bytes=index.encode();served['/'+label+'/index.toml']=index_bytes
    served['/'+label+'/pack.toml']=('name = "Synthetic duplicate ownership"\npack-format = "packwiz:1.1.0"\nversion = "'+label+'"\n[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = "'+sha(index_bytes)+'"\n').encode()

duplicate_pack('duplicate-old','1')
duplicate_pack('duplicate-retired-unchanged','1',retire=True)
duplicate_pack('duplicate-retired-updated','2',retire=True)
duplicate_pack('duplicate-unsafe-remove','1',remove=True)

def run(instance,label):
    instance.mkdir(parents=True,exist_ok=True)
    result=subprocess.run([JAVA,'-Djava.awt.headless=true','-cp',JAR,'link.infra.packwiz.installer.Main','-g','--pack-folder',str(instance),'--multimc-folder',str(root/'no-launcher'),base+'/'+label+'/pack.toml'],cwd=instance,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=30)
    if result.returncode:
        print(result.stdout.decode(errors='replace')[-6000:]);raise AssertionError('Installer failed: '+label)
    return result.stdout.decode(errors='replace')
def seed_personal(instance):
    path=instance/'xaero/a.xwmc';path.write_bytes(b'SYNTHETIC PERSONAL CACHE MODIFIED AFTER INITIAL INSTALL\x00\xff')
    extra=instance/'xaero/unmanaged-test.bin';extra.write_bytes(b'SYNTHETIC UNMANAGED SENTINEL')
    return {p.relative_to(instance).as_posix():sha(p.read_bytes()) for p in [path,extra]}
def unchanged(instance,snapshot):
    for rel,digest in snapshot.items():assert sha((instance/rel).read_bytes())==digest
try:
    upgraded=root/'upgraded';run(upgraded,'old');personal=seed_personal(upgraded)
    before=len(requests);run(upgraded,'transition');unchanged(upgraded,personal)
    assert (upgraded/'mods/synthetic.bin').read_bytes()==b'synthetic-mod-2'
    assert all('/xaero/' not in path for path in requests[before:])
    manifest=json.loads((upgraded/'packwiz.json').read_text());entry=manifest['cachedFiles']['xaero/a.xwmc']
    assert entry['cachedLocation']=='xaero/a.xwmc','Compatibility tombstone must be documented as still owned'
    print('PASS installed v0.5.14 old->directory-alias/preserve: cache and unmanaged sentinel byte hashes unchanged; mod updated; zero private-URI requests; ownership retained')
    before=len(requests);run(upgraded,'transition');run(upgraded,'later');unchanged(upgraded,personal)
    assert all('/xaero/' not in path for path in requests[before:]);assert (upgraded/'mods/synthetic.bin').read_bytes()==b'synthetic-mod-3'
    print('PASS repeated update and later changed index retain synthetic personal cache without downloads')
    skipped=root/'skipped';run(skipped,'old');skipped_hashes=seed_personal(skipped);before=len(requests);run(skipped,'later');unchanged(skipped,skipped_hashes)
    assert all('/xaero/' not in path for path in requests[before:])
    print('PASS skipped intermediate release: old installation directly reaches later tombstone index without private-file mutation or request')
    fresh=root/'fresh';before=len(requests);run(fresh,'later');run(fresh,'later')
    assert not (fresh/'xaero/a.xwmc').exists();assert not (fresh/'xaero').exists();assert all('/xaero/' not in path for path in requests[before:])
    fresh_manifest=json.loads((fresh/'packwiz.json').read_text());assert fresh_manifest['cachedFiles']['xaero/a.xwmc'].get('cachedLocation') is None
    print('PASS fresh install and repeat: no private cache/parent directory created and zero private-URI requests')
    control=root/'deletion-control';shutil.copytree(upgraded,control);run(control,'unsafe-clean')
    assert not (control/'xaero/a.xwmc').exists();assert (control/'xaero/unmanaged-test.bin').exists()
    print('PASS destructive control on synthetic copy: removing retained URI actually deletes cached personal file; untracked sentinel survives')
    duplicate=root/'duplicate-owned'
    # Model an already-installed shared JAR; two fresh concurrent downloads race in upstream v0.5.14.
    (duplicate/'mods').mkdir(parents=True);(duplicate/'mods/shared.jar').write_bytes(b'synthetic-shared-1')
    run(duplicate,'duplicate-old')
    duplicate_manifest=json.loads((duplicate/'packwiz.json').read_text())
    assert duplicate_manifest['cachedFiles']['mods/primary.pw.toml']['cachedLocation']=='mods/shared.jar'
    assert duplicate_manifest['cachedFiles']['mods/.index/duplicate.pw.toml']['cachedLocation']=='mods/shared.jar'
    duplicate_control=root/'duplicate-delete-control';shutil.copytree(duplicate,duplicate_control)
    before=len(requests);run(duplicate,'duplicate-retired-unchanged')
    assert (duplicate/'mods/shared.jar').read_bytes()==b'synthetic-shared-1'
    assert not any('/mods/.index/' in path for path in requests[before:])
    run(duplicate,'duplicate-retired-updated')
    assert (duplicate/'mods/shared.jar').read_bytes()==b'synthetic-shared-2'
    print('PASS duplicate metadata ownership: retired alias preserves unchanged primary JAR and allows a primary update, without fetching retired metadata')
    run(duplicate_control,'duplicate-unsafe-remove')
    assert not (duplicate_control/'mods/shared.jar').exists()
    print('PASS duplicate destructive control: removing duplicate URI deletes the unchanged primary JAR after its cache validation')
    print('PASS installer version v0.5.14; SHA256 '+sha(pathlib.Path(JAR).read_bytes()))
    if args.keep: print('EVIDENCE '+str(root))
finally:
    server.shutdown();server.server_close()
    if not args.keep: shutil.rmtree(root)
