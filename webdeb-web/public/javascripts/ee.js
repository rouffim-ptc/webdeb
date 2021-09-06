/*
 * WebDeb - Copyright (C) <2014-2019> <Université catholique de Louvain (UCL), Belgique ; Université de Namur (UNamur), Belgique>
 *
 * List of the contributors to the development of WebDeb: see AUTHORS file.
 * Description and complete License: see LICENSE file.
 *
 * This program (WebDeb) is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see COPYING file).  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 */

"use strict";

/*!
 * ee v1.0 is a jQuery extension.
 * https://github.com/martini224
 *
 * Includes jquery.js
 * https://jquery.com/
 *
 * Copyright Martin Rouffiange (martini224) 2018
 * Released under the MIT license (http://opensource.org/licenses/MIT)
 */
(function(e) { e.fn.easter = function () {

    let eOptions,
        eBar,
        container = $(this),
        header_container,
        score,
        snake,
        board,
        nbLines,
        nbColumns,
        directions,
        box_types,
        score_types,
        ended,
        speed_max = 101,
        speed_min = 10,
        speed = speed_max,
        tspeed,
        time_max = 30000,
        time,
        time_min = 3000,
        time_step = 100,
        time_spawn_apple_yellow_max = 7500,
        time_spawn_apple_yellow,
        nb_apple_red_spawn_max = 10,
        nb_apple_red_spawn,
        apple_yellow_box,
        tapple_yellow,
        box_size = 15;

    $(document).ready(function() {
        initEnums();

        createBoard();

        initListeners();
    });

    function initEnums(){

        directions = {
            LEFT : 1,
            RIGHT : 2,
            UP : 3,
            DOWN : 4
        };

        box_types = {
            BLANK : 1,
            SNAKE_HEAD : 2,
            SNAKE_BODY : 3,
            APPLE_RED : 4,
            APPLE_YELLOW : 5
        };

        score_types = {
            APPLE_RED: 1,
            APPLE_YELLOW: 10
        }
    }

    function initListeners(){
        /*
         * Rebuild chart on screen resize
         */
        $(window).on('resize', function(){
            if(snake !== undefined)
            snake.end();
            createBoard();
        });

        $(document).keydown(function(e) {
            if(ended){
                initGame();
            }else {
                switch (e.which) {
                    case 37: // left
                        if (snake.direction !== directions.RIGHT)
                            snake.changeDirection(directions.LEFT);
                        break;

                    case 38: // up
                        if (snake.direction !== directions.DOWN)
                            snake.changeDirection(directions.UP);
                        break;

                    case 39: // right
                        if (snake.direction !== directions.LEFT)
                            snake.changeDirection(directions.RIGHT);
                        break;

                    case 40: // down
                        if (snake.direction !== directions.UP)
                            snake.changeDirection(directions.DOWN);
                        break;

                    default:
                        return; // exit this handler for other keys
                }
            }
            e.preventDefault(); // prevent the default action
        });

        container.click(function(e) {
            let middle_x = container.width() / 2;
            let middle_y = container.height() / 2;

            if(snake.direction === directions.UP || snake.direction === directions.DOWN) {
                snake.changeDirection(e.pageX > middle_x ? directions.RIGHT : directions.LEFT);
            } else {
                snake.changeDirection(e.pageY > middle_y ? directions.DOWN : directions.UP);
            }
        });
    }

    function createBoard(){
        container.empty();
        nbLines = Math.floor((container.height() - 30) / box_size);
        nbColumns = Math.floor(container.width() / box_size);
        board = [];
        eBar = {};

        if(nbLines >= 10 && nbColumns >=0) {
            header_container = $('<div class="snake_header"></div>').appendTo(container);

            let table = $('<table class="snake-table"></table>').appendTo($('<div class="snake-sub-container"></div>').appendTo(container));
            $('<span style="margin-left: 15px">score : </span>').appendTo(header_container);
            eBar.score = $('<span></span>').appendTo(header_container);
            $('<span style="margin-left: 15px">speed : </span>').appendTo(header_container);
            eBar.speed = $('<span></span>').appendTo(header_container);

            for (let iLine = 0; iLine < nbLines; iLine++) {
                let line = $('<tr style="height:' + box_size + 'px"></tr>').appendTo(table);
                board[iLine] = [];

                for (let iColumn = 0; iColumn < nbColumns; iColumn++) {
                    board[iLine][iColumn] = $('<td style="width:' + box_size + 'px"></td>').appendTo(line);
                }
            }

            initGame();
        }
    }

    function initGame(){
        score = 0;
        speed = speed_max;
        time = time_min;
        time_spawn_apple_yellow = time_spawn_apple_yellow_max;
        nb_apple_red_spawn = 0;
        ended = false;

        updateHeader();
        changeBackgroundColor(true);

        snake = new Snake(Math.floor(nbColumns / 2), Math.floor(nbLines / 2));

        generateApple();
        decreaseSpeed();
    }

    function changeBackgroundColor(played){
        container.find("table").toggleClass("played", played);
        container.find("table").toggleClass("ended", !played);
    }

    function updateHeader(){
        eBar.score.text(score);
        eBar.speed.text(speed_max - speed);
    }

    function setBoardElement(posX, posY, type){
        let box = board[posY][posX];
        if(box !== undefined){
            box.removeClass();

            switch (type) {
                case box_types.SNAKE_HEAD :
                    box.addClass("snake snake-head");
                    break;
                case box_types.SNAKE_BODY :
                    box.addClass("snake snake-body");
                    break;
                case box_types.APPLE_RED :
                    box.addClass("apple apple-red");
                    break;
                case box_types.APPLE_YELLOW :
                    box.addClass("apple apple-yellow");
                    break;
            }
        }
    }

    function determineAppleType(posX, posY){
        let box = board[posY][posX];

        if(box !== undefined) {
            if(box.hasClass("apple-red")) return box_types.APPLE_RED;
            if(box.hasClass("apple-yellow")) return box_types.APPLE_YELLOW;
        }

        return box_types.BLANK;
    }

    function boardBoxIsFree(posX, posY){
        return posX >= 0 && posX < nbColumns && posY >= 0 && posY < nbLines && !board[posY][posX].hasClass("snake");
    }

    function boardBoxIsApple(posX, posY){
        return posX >= 0 && posX < nbColumns && posY >= 0 && posY < nbLines && board[posY][posX].hasClass("apple");
    }

    function end(){
        ended = true;
        clearTimeout(tspeed);
    }

    function generateApple(type){
        type = type || box_types.APPLE_RED;
        let posX;
        let posY;

        do {
            posX = Math.floor(Math.random() * nbColumns);
            posY = Math.floor(Math.random() * nbLines);
        } while (!boardBoxIsFree(posX, posY));
        setBoardElement(posX, posY, type);

        switch(type){
            case box_types.APPLE_RED :
                if(++nb_apple_red_spawn > nb_apple_red_spawn_max){
                    nb_apple_red_spawn = 0;
                    generateApple(box_types.APPLE_YELLOW);
                }
                break;
            case box_types.APPLE_YELLOW :
                apple_yellow_box = new SnakeBody(posX, posY);
                tapple_yellow = setTimeout(removeAppleYellow, time_spawn_apple_yellow);
                break;
        }
    }

    function removeAppleYellow(){
        setBoardElement(apple_yellow_box.posX, apple_yellow_box.posY, box_types.BLANK);
    }

    function decreaseSpeed(){
        if(speed > speed_min)
            speed--;

        updateHeader();
        tspeed = setTimeout(decreaseSpeed, time);

        if(time + time_step < time_max)
            time += time_step;
    }

    class Snake{
        constructor(posX, posY){
            this.head = new SnakeBody(posX, posY);
            this.body = [];
            this.body.push(this.head);
            this.direction = directions.LEFT;
            this.go();
            this.tid = null;
        }

        go(){
            let that = this;
            that.locked = false;

            switch (that.direction) {
                case directions.LEFT :
                    that.goLeft();
                    break;
                case directions.RIGHT :
                    that.goRight();
                    break;
                case directions.DOWN :
                    that.goDown();
                    break;
                default :
                    that.goUp();
                    break;
            }

            this.tid = setTimeout(function(){
                that.go();
            }, speed);
        }

        goLeft(){
            let posX = this.head.posX === 0 ? nbColumns - 1 : this.head.posX - 1;
            if(boardBoxIsFree(posX, this.head.posY)){
                this.move(posX, this.head.posY);
            }else {
                this.end();
            }
        }

        goRight(){
            let posX = this.head.posX === nbColumns - 1 ? 0 : this.head.posX + 1;
            if(boardBoxIsFree(posX, this.head.posY)){
                this.move(posX, this.head.posY);
            }else {
                this.end();
            }
        }

        goUp(){
            let posY = this.head.posY === 0 ? nbLines - 1 : this.head.posY - 1;
            if(boardBoxIsFree(this.head.posX, posY)){
                this.move(this.head.posX, posY);
            }else {
                this.end();
            }
        }

        goDown(){
            let posY = this.head.posY === nbLines - 1 ? 0 : this.head.posY + 1;
            if(boardBoxIsFree(this.head.posX, posY)){
                this.move(this.head.posX, posY);
            }else {
                this.end();
            }
        }

        move(posX, posY){
            let eated = false;

            if(boardBoxIsApple(posX, posY)){
                this.eat(posX, posY);
                eated = true;
            }

            setBoardElement(this.head.posX, this.head.posY, this.body.length <= 1 ? box_types.BLANK :box_types.SNAKE_BODY);
            let newBody = new SnakeBody(posX, posY);
            this.head = newBody;
            this.body.unshift(newBody);
            setBoardElement(posX, posY, box_types.SNAKE_HEAD);

            if(!eated){
                setBoardElement(this.body[this.body.length - 1].posX, this.body[this.body.length - 1].posY, box_types.BLANK);
                this.body.pop();
            }
        }

        changeDirection(direction){
            if(!this.locked) {
                this.direction = direction;
                this.locked = true;
            }
        }

        end(){
            this.locked = true;
            if(this.tid != null){
                clearTimeout(this.tid);
            }
            end();
        }

        eat(posX, posY){
            switch(determineAppleType(posX, posY)){
                case box_types.APPLE_RED :
                    score += score_types.APPLE_RED;
                    generateApple();
                    break;
                case box_types.APPLE_YELLOW :
                    score += score_types.APPLE_YELLOW;
                    break;
            }
            updateHeader();
        }
    }

    class SnakeBody{
        constructor(posX, posY){
            this.posX = posX;
            this.posY = posY;
        }
    }


};}(jQuery));