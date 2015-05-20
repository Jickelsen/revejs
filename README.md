# revejs

A [ClojureScript](https://github.com/clojure/clojurescript) game made using [Quil](http://quil.info/) and the [Brute](https://github.com/markmandel/brute) entity component system library. Uses [Figwheel](https://github.com/bhauman/lein-figwheel) for sweet interactive playtesting!

## Usage

### Get it up and running
Run `lein figwheel` and go to http://localhost:3449/. All changes will be reflected on save. Enter will reset the game state. In emacs you can use `cider-connect` to localhost:7888 to connect to the figwheel repl. To then start a cljs repl issue `(use 'figwheel-sidecar.repl-api)` followed by `(cljs-repl)`.

### Playing the game
Use WASD to control the green ship. Q reverses time for the ship (position state undo!) and C fires the cannon.
Arrows control the blue ship with shift engaging reverse time and . (period) firing the cannon.

Beware of friendly fire!

## License

Copyrigh © 2015 Jacob Michelsen 

Distributed under the Eclipse Public License either version 1.0 or any later version.
