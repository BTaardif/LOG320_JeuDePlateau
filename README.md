# <p align="center">Laboratoire : Jeu de Plateau </p>

Le jeu de tic-tac-toe, aussi nommé « morpion » en France et « oxo » en Belgique, est bien connu. Ce jeu de réflexion 
à deux joueurs se joue sur un plateau de 3x3 et le but consiste à aligner 3 pièces du même joueur de façon 
horizontale, verticale ou diagonale. Il est relativement simple pour un ordinateur de jouer parfaitement et ainsi 
gagner ou annuler à toutes les parties. Dans le cadre de ce laboratoire, nous allons utiliser une version plus complexe 
dans laquelle chaque case du tic-tac-toe est un autre plateau de tic-tac-toe. Il s’agit donc de deux plateaux imbriqués. 
Ceci rend l’arbre de recherche plus complexe et une stratégie plus complexe, incluant une vue globale du jeu, doit 
être définie.
Les objectifs de ce laboratoire sont :
• Implémentation de l’algorithme minimax et alpha-beta
• Implémentation d’heuristiques pour le jeu de Tic tac toe géant
• Exploration et implémentation de différentes stratégies afin d’améliorer l’efficacité de votre algorithme de 
décision.

# <p align="center">Introduction </p>
<p align="center">Note sur l'énoncé </p>


### Objectifs
- Implémentation de l’algorithme minimax et alpha-beta
Benjamin : mon élagage pour le alpha-beta ne fonctionnait pas. Si quelqu’un a eu tous ses points pour ça je propose qu’on prenne celui là.
#####
- Implémentation d’heuristiques pour le jeu de tic tac toe géant
Heuristique : qui sert à découvrir. Découvrir les possibilités des coûts du tic tac toe
#####
- Exploration et implémentation de différentes stratégies afin d’améliorer l’efficacité de votre algorithme de décision.
En apprendre sur le jeu de tic tac toe pour pouvoir créer un algorithme qui se démarque. On ne peut pas juste créer une algorithme minimax et espérer qu’il gagne contre les autres algorithmes minimax.
##
### INTERFACE
 
-	Implémenter un interface graphique minimal dans le terminal pour suivre le déroulement de la partie.
#####
-	L’interface graphique doit pouvoir laisser l’humain décider s’il veut jouer avec les X ou les O. Cela peut être fait avec une fenêtre au début du jeu ou dans le terminal.
#####
-	Il faut implémenter le fait que le serveur prévoit dans quel jeu il faut jouer. (Giant Board vs innerBoard)
##
### FONCTIONS À IMPLÉMENTER
 
##### Générateur de mouvements : 
- Génère tous les coûts possibles. 
####
##### Fonction d’évaluation : 
- Évaluation des coûts à créer avec l’heuristique et l’exploration des différentes stratégies mentionnée à l’introduction.
#### 
##### Algorithme MinMax et AlphaBeta
- Laboratoire 1
  
###
### 
###
# <p align="center">Point de Contrôle 1 : 14 mars 2024 </p>
###  
Notre AI doit :

    - Se connecter au serveur 🟢
    - Jouer en X et O     🟡
    - Avoir implémenter le générateur de mouvement   🟡 
    - Avoir implémenter une fonction d'évaluation (Pas très intelligente)     🟡 
    - Avoir implémenter l'algorithme MinMax Alpha Beta (en situation de jeu)     🟡