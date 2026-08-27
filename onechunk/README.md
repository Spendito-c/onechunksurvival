# OneChunkSurvival

Plugin custom pour un serveur Paper/Spigot : chaque joueur demarre isole dans
son propre monde prive, spawn toujours a (0,0), entoure d'un mur invisible.
En minant (bois, pierre, minerais...), il gagne de l'XP qui fait grandir
sa zone, stade par stade, a l'infini.

## Fonctionnement

1. A la premiere connexion, le plugin cree un monde dedie au joueur
   (`chunk_<uuid-du-joueur>`) et le teleporte a (0,0).
2. Il verifie qu'il y a au moins 3 rondins de bois et de la pierre accessibles
   pres du spawn. Si ce n'est pas le cas (biome sans arbre, etc.), il genere
   un petit arbre et un patch de pierre de secours pour garantir un debut
   jouable.
3. Deux objectifs de depart s'affichent : recuperer 3 rondins et miner 5
   blocs de pierre.
4. Chaque bloc mine (configurable dans config.yml) donne de l'XP.
5. Quand l'XP atteint le seuil du stade actuel, le joueur passe au stade
   suivant : le rayon de sa zone augmente, et le seuil du prochain stade
   est plus eleve (progression infinie).
6. Un mur invisible bloque toute sortie de la zone actuelle : le joueur est
   repousse et prevenu par un message.

## Compiler le plugin

Mon environnement n'a pas acces au repository Maven de PaperMC, donc je
n'ai pas pu generer le .jar directement. Voici comment le faire toi-meme,
c'est rapide :

### Option A - Avec IntelliJ IDEA (le plus simple)
1. Installe IntelliJ IDEA Community (gratuit).
2. Ouvre le dossier `onechunk` en tant que projet Maven (il detectera le
   pom.xml automatiquement et telechargera les dependances).
3. Une fois indexe, ouvre l'onglet Maven (a droite) > Lifecycle > double-clic
   sur `package`.
4. Le fichier `OneChunkSurvival.jar` sera genere dans le dossier `target/`.

### Option B - Avec Maven en ligne de commande
Si tu as Java 21+ et Maven installes sur ton PC :
```
cd onechunk
mvn clean package
```
Le jar sera dans `target/OneChunkSurvival.jar`.

### Option C - Si tu n'as aucun outil de dev
Dis-le moi et je peux :
- te fournir un guide pas a pas pour installer Java + Maven rapidement, ou
- essayer de compiler autrement si un acces reseau different devient
  disponible de mon cote.

## Installation sur le serveur

1. Copie `OneChunkSurvival.jar` (une fois compile) dans le dossier `plugins/`
   de ton serveur Paper/Purpur (1.21+).
2. Redemarre le serveur.
3. Un fichier `plugins/OneChunkSurvival/config.yml` sera genere - ajuste les
   valeurs si besoin (voir ci-dessous).

## Configuration (config.yml)

- `start-radius` : rayon de depart en blocs (8 = zone d'environ 16x16, soit
  un chunk).
- `radius-increase-per-stage` : de combien de blocs le rayon augmente a
  chaque stade.
- `xp-required-base` / `xp-required-growth` : seuil d'XP necessaire pour
  chaque stade (formule : base + growth * (stade - 1)). Par defaut le
  premier stade demande 8 XP, soit exactement 3 rondins (3xp) + 5 pierres
  (5xp).
- `xp-per-block` : liste des blocs qui donnent de l'XP quand mines, et
  combien. Ajoute ou retire des blocs comme tu veux (minerais precieux
  peuvent donner plus d'XP par exemple).

## Commande

- `/mychunk` : affiche le stade actuel, le rayon, l'XP et l'avancement des
  objectifs de depart.

## Limites connues / ameliorations possibles

- Chaque monde prive reste charge en permanence une fois cree (pas de
  dechargement automatique si le joueur est hors ligne longtemps). Sur un
  serveur avec beaucoup de joueurs, ca peut consommer de la RAM avec le
  temps. Dis-moi si tu veux que j'ajoute un dechargement automatique des
  mondes inactifs.
- Le mur est un carre (base sur le rayon en X/Z), pas un cercle.
- Pas encore de systeme de reset/suppression de zone via commande admin -
  je peux l'ajouter si utile.
